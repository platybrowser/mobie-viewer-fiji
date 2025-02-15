/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package org.embl.mobie.command.context;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import ij.IJ;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import org.embl.mobie.command.CommandConstants;
import org.embl.mobie.lib.util.Corners;
import org.embl.mobie.lib.util.MoBIEHelper;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.bdvpg.bdv.BdvHandleHelper;
import sc.fiji.bdvpg.scijava.command.BdvPlaygroundActionCommand;

import java.util.Arrays;

@Plugin(type = BdvPlaygroundActionCommand.class, menuPath = CommandConstants.CONTEXT_MENU_ITEMS_ROOT + "Select Box")
public class BoxSelectionCommand implements BdvPlaygroundActionCommand
{
    static { net.imagej.patcher.LegacyInjector.preinit(); }

    @Parameter
    public BdvHandle bdvHandle;

    @Override
    public void run()
    {
        // Compute the maximal and initial size of the box
        Corners globalCorners = MoBIEHelper.getBdvWindowGlobalCorners( bdvHandle );
        double[] range = new double[ 3 ];
        range[ 0 ] = LinAlgHelpers.distance( globalCorners.upperLeft, globalCorners.upperRight );
        range[ 1 ] = LinAlgHelpers.distance( globalCorners.upperLeft, globalCorners.lowerLeft );
        range[ 2 ] = Math.min( range[ 0 ], range[ 1 ] );

        RealInterval rangeInterval = new FinalRealInterval(
                Arrays.stream( range ).map( x -> -x ).toArray(),
                Arrays.stream( range ).map( x -> x ).toArray() );

        RealInterval initialInterval = new FinalRealInterval(
                Arrays.stream( range ).map( x -> -x / 4.0 ).toArray(),
                Arrays.stream( range ).map( x -> x / 4.0 ).toArray() );

        // Compute transform of interval to global coordinate system
        final AffineTransform3D currentRotation = MoBIEHelper.getCurrentViewerRotation( bdvHandle );
        double[] centre = BdvHandleHelper.getWindowCentreInCalibratedUnits( bdvHandle );
        final AffineTransform3D translateOriginToCenter = new AffineTransform3D();
        translateOriginToCenter.translate( centre );
        final AffineTransform3D boxTransform = currentRotation.inverse().copy().preConcatenate( translateOriginToCenter );

        // Show box in BDV
        final TransformedRealBoxSelectionDialog.Result result = BdvFunctions.selectRealBox(
                bdvHandle,
                boxTransform,
                initialInterval,
                rangeInterval,
                BoxSelectionOptions.options().title( "Select box" ) );

        if ( result.isValid() )
        {
            IJ.log( "# Selected box" );
            RealInterval interval = result.getInterval();
            double[] boxSize = MoBIEHelper.getSize( interval );
            double[] globalCenter = new double[ 3 ];
            boxTransform.apply( MoBIEHelper.getCenter( interval ), globalCenter );
            IJ.log( "Center in global coordinate system: " + Arrays.toString( globalCenter ) );
            IJ.log( "Size: " + Arrays.toString( boxSize ) );
            IJ.log( "Interval: " + Arrays.toString( interval.minAsDoubleArray() ) + ", " + Arrays.toString( interval.maxAsDoubleArray() ) );
            IJ.log( "Interval transform: " + Arrays.toString( boxTransform.getRowPackedCopy() ) );
        }
    }

    private RealInterval getRangeInterval( )
    {
        final FinalRealInterval viewerBoundingInterval = BdvHandleHelper.getViewerGlobalBoundingInterval( bdvHandle );
        double[] centre = new double[ 3 ];
        double[] size = new double[ 3 ];

        for (int d = 0; d < 3; d++)
        {
            centre[ d ] = ( viewerBoundingInterval.realMax( d ) + viewerBoundingInterval.realMin( d ) ) / 2.0;
            size[ d ] = ( viewerBoundingInterval.realMax( d ) - viewerBoundingInterval.realMin( d ) );
        }

        size[ 2 ] = Math.min( size[ 0 ], size[ 1 ] );

        double[] minBB = new double[3];
        double[] maxBB = new double[3];

        for ( int d = 0; d < 3; d++ )
        {
            minBB[ d ] = centre[ d ] - size[ d ] / 2;
            maxBB[ d ] = centre[ d ] + size[ d ] / 2;
        }

        return new FinalRealInterval( minBB, maxBB );
    }

}
