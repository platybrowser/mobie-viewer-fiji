package org.embl.mobie.viewer.table;

import net.imglib2.realtransform.AffineTransform3D;
import org.embl.mobie.viewer.annotation.Annotation;
import net.imglib2.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConcatenatedAnnotationTableModel< A extends Annotation > implements AnnotationTableModel< A >
{
	private final Set< AnnotationTableModel< A > > tableModels;
	private AnnotationTableModel< A > referenceTable;
	private HashMap< A, Integer > annotationToRowIndex = new HashMap<>();
	private HashMap< Integer, A > rowIndexToAnnotation = new HashMap<>();
	private Set< AnnotationTableModel > loadedTables = new HashSet<>();
	private int numRows = 0;

	public ConcatenatedAnnotationTableModel( Set< AnnotationTableModel< A > > tableModels )
	{
		this.tableModels = tableModels;
		this.referenceTable = tableModels.iterator().next();
	}

	private HashMap< A, Integer > getAnnotationToRowIndex()
	{
		update();
		return annotationToRowIndex;
	}

	private synchronized void update()
	{
		for ( AnnotationTableModel< A > tableModel : tableModels )
		{
			if ( loadedTables.contains( tableModel ) ) continue;

			if ( tableModel.isDataLoaded() )
			{
				final Set< A > rows = tableModel.annotations();
				for ( A row : rows )
				{
					annotationToRowIndex.put( row, numRows );
					rowIndexToAnnotation.put( numRows, row );
					numRows++;
				}
				loadedTables.add( tableModel );
			}
		}
	}

	@Override
	public List< String > columnNames()
	{
		return referenceTable.columnNames();
	}

	@Override
	public List< String > numericColumnNames()
	{
		return referenceTable.numericColumnNames();
	}

	@Override
	public Class< ? > columnClass( String columnName )
	{
		return referenceTable.columnClass( columnName );
	}

	@Override
	public int numAnnotations()
	{
		update();
		return numRows;
	}

	@Override
	public int rowIndexOf( A annotation )
	{
		return getAnnotationToRowIndex().get( annotation );
	}

	@Override
	public A annotation( int rowIndex )
	{
		// We do not update the tables here,
		// because one should only ask for
		// rows with an index lower than the
		// current numRows.
		return rowIndexToAnnotation.get( rowIndex );
	}

	@Override
	public void requestColumns( String columnsPath )
	{
		for ( AnnotationTableModel< A > tableModel : tableModels )
			tableModel.requestColumns( columnsPath );
	}

	@Override
	public void setAvailableColumnPaths( Set< String > availableColumnPaths )
	{
		for ( AnnotationTableModel< A > tableModel : tableModels )
			tableModel.setAvailableColumnPaths( availableColumnPaths );
	}

	@Override
	public Collection< String > availableColumnPaths()
	{
		return referenceTable.availableColumnPaths();
	}

	@Override
	public LinkedHashSet< String > loadedColumnPaths()
	{
		return referenceTable.loadedColumnPaths();
	}

	@Override
	public Pair< Double, Double > getMinMax( String columnName )
	{
		return getColumnMinMax( columnName, annotations() );
	}

	@Override
	public Set< A > annotations()
	{
		return getAnnotationToRowIndex().keySet();
	}

	@Override
	public void addStringColumn( String columnName )
	{
		// here we probably need to load all tables
		throw new UnsupportedOperationException("Annotation of concatenated tables is not yet implemented.");
	}

	@Override
	public boolean isDataLoaded()
	{
		// note that here this does not mean that
		// all data is loaded...
		return referenceTable.isDataLoaded();
	}

	@Override
	public String dataStore()
	{
		return referenceTable.dataStore();
	}

	@Override
	public void transform( AffineTransform3D affineTransform3D )
	{
		for ( AnnotationTableModel< A > tableModel : tableModels )
			tableModel.transform( affineTransform3D );
	}
}
