/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.lib.source;

import bdv.AbstractSpimSource;
import bdv.SpimSource;
import bdv.tools.transformation.TransformedSource;
import bdv.util.Affine3DHelpers;
import bdv.util.ResampledSource;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.RealMaskRealInterval;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.WritableBox;
import org.embl.mobie.lib.image.StitchedImage;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SourceHelper
{
    // TODO: one could get rid of this if Sources could
    //   return their mask directly with some other method!
	//   Is this still used?
	@Deprecated
    public static double[][] getMinMax( Source< ? > source )
    {
        final double[][] minMax = new double[2][];
		final RandomAccessibleInterval< ? > rai = source.getSource( 0, 0 );
		minMax[ 0 ] = rai.minAsDoubleArray();
		minMax[ 1 ] = rai.maxAsDoubleArray();
        return minMax;
    }

	public static < T > T unwrapSource( Source source, Class< T > clazz )
	{
		if ( source == null )
			return null;

		if ( clazz.isInstance( source ) )
		{
			return ( T ) source;
		}
		else if ( source instanceof TransformedSource )
		{
			final Source< ? > wrappedSource = ( ( TransformedSource< ? > ) source ).getWrappedSource();
			return unwrapSource( wrappedSource, clazz );
		}
		else if ( source instanceof SourceWrapper )
		{
			final Source< ? > wrappedSource = ( ( SourceWrapper< ? > ) source ).getWrappedSource();
			return unwrapSource( wrappedSource, clazz );
		}
		else
		{
			return null;
		}
	}

	public static int getNumTimepoints( Source< ? > source )
	{
		int numSourceTimepoints = 0;
        final int maxNumTimePoints = 10000; // TODO
        for ( int t = 0; t < maxNumTimePoints; t++ )
		{
			if ( source.isPresent( t ) )
            {
                numSourceTimepoints++;
            }
            else
            {
                return numSourceTimepoints;
            }
		}

        if ( numSourceTimepoints == maxNumTimePoints )
		{
			System.err.println( source.getName() + " has more than " + maxNumTimePoints + " time-points. Is this an error?!" );
		}

		return numSourceTimepoints;
	}

	/**
	 * Recursively fetch all root sources
	 * @param source
	 * @param rootSources
	 */
	public static void fetchRootSources( Source< ? > source, Set< Source< ? > > rootSources )
	{
		if ( source instanceof SpimSource )
		{
			rootSources.add( source );
		}
		else if ( source instanceof TransformedSource )
		{
			final Source< ? > wrappedSource = ( ( TransformedSource ) source ).getWrappedSource();

			fetchRootSources( wrappedSource, rootSources );
		}
		else if (  source instanceof SourceWrapper )
		{
			final Source< ? > wrappedSource = (( SourceWrapper ) source).getWrappedSource();

			fetchRootSources( wrappedSource, rootSources );
		}
		// TODO: how do do this now? Do we still need it?
//		else if (  source instanceof MergedGridSource )
//		{
//			final MergedGridSource< ? > mergedGridSource = ( MergedGridSource ) source;
//			final List< ? extends SourceAndConverter< ? > > gridSources = mergedGridSource.getGridSources();
//			for ( SourceAndConverter< ? > gridSource : gridSources )
//			{
//				fetchRootSources( gridSource.getSpimSource(), rootSources );
//			}
//		}
		else if ( source instanceof StitchedImage )
		{
			final StitchedImage< ?, ? > stitchedImage = ( StitchedImage ) source;
			final List< ? extends Source< ? > > gridSources = stitchedImage.getImages().stream().map( image -> image.getSourcePair().getSource() ).collect( Collectors.toList() );
			for ( Source< ? > gridSource : gridSources )
			{
				fetchRootSources( gridSource, rootSources );
			}
		}
		else if (  source instanceof ResampledSource )
		{
			final ResampledSource resampledSource = ( ResampledSource ) source;
			final Source< ? > wrappedSource = resampledSource.getOriginalSource();
			fetchRootSources( wrappedSource, rootSources );
		}
		else
		{
			throw new IllegalArgumentException("For sources of type " + source.getClass().getName() + " the root source currently cannot be determined.");
		}
	}

	public static RealMaskRealInterval getMask( Source< ? > source, int t )
	{
		// fetch the extent of the source in voxel space
		final double[] min = new double[ 3 ];
		final double[] max = new double[ 3 ];

		final Masked masked = SourceHelper.unwrapSource( source, Masked.class );
		if ( masked != null )
		{
			final RealInterval realInterval = masked.getMask();
			realInterval.realMin( min );
			realInterval.realMax( max );
		}
		else
		{
			final RandomAccessibleInterval< ? > rai = source.getSource( t, 0 );
			rai.realMin( min );
			rai.realMax( max );
		}
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		source.getSourceTransform( 0, 0, sourceTransform );

		// expand with voxel dimensions
		final double[] voxelSizes = new double[ 3 ];
		source.getVoxelDimensions().dimensions( voxelSizes );
		for ( int d = 0; d < 3; d++ )
		{
			min[ d ] -= voxelSizes[ d ];
			max[ d ] += voxelSizes[ d ];
		}

		// create mask
		// as compared with estimateBounds this has the
		// advantage that it can represent a rotated box
		final RealMaskRealInterval mask = GeomMasks.closedBox( min, max ).transform( sourceTransform.inverse() );
		return mask;
	}

	public static RealMaskRealInterval estimateMask( Source< ? > source, int t, boolean includeVoxelDimensions )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		source.getSourceTransform( t, 0, sourceTransform );

		// determine the extent of the source in voxel space
		//
		final RandomAccessibleInterval< ? > rai = source.getSource( t, 0 );
		final double[] min = rai.minAsDoubleArray();
		final double[] max = rai.maxAsDoubleArray();

		// extend the bounds in voxel space to include the voxel dimensions
		if ( includeVoxelDimensions )
		{
			final double[] voxelDimensions = source.getVoxelDimensions().dimensionsAsDoubleArray();
			for ( int d = 0; d < min.length; d++ )
			{
				final double scale = Affine3DHelpers.extractScale( sourceTransform, d );
				min[ d ] -= voxelDimensions[ d ] / scale;
				max[ d ] += voxelDimensions[ d ] / scale;
			}
		}
		WritableBox box = GeomMasks.closedBox( min, max );

		// apply the source transformation to the voxel space box
		// to get the mask in calibrated space
		final RealMaskRealInterval realInterval = box.transform( sourceTransform.inverse() );

		return realInterval;
	}

	public static FinalRealInterval bounds( Source< ? > source, int t )
	{
		final AffineTransform3D affineTransform3D = new AffineTransform3D();
		source.getSourceTransform( 0, 0, affineTransform3D );
 		final RandomAccessibleInterval< ? > rai = source.getSource( t, 0 );
		final double[] min = rai.minAsDoubleArray();
		final double[] max = rai.maxAsDoubleArray();
		final FinalRealInterval bounds = affineTransform3D.estimateBounds( rai );
		return bounds;
	}

	public static void setVoxelDimensionsToPixels( AbstractSpimSource< ? > source )
	{
		try
		{
			final VoxelDimensions pixelDimensions = new FinalVoxelDimensions( "pixel", 1.0, 1.0, 1.0 );
			Field voxelDimensions = AbstractSpimSource.class.getDeclaredField("voxelDimensions");
			voxelDimensions.setAccessible(true);
			voxelDimensions.set( source, pixelDimensions );
		} catch ( NoSuchFieldException e )
		{
			e.printStackTrace();
		} catch ( IllegalAccessException e )
		{
			e.printStackTrace();
		}
	}
}
