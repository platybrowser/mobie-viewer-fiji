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
package projects.muscle_patterning_drosophila;

import net.imagej.ImageJ;
import org.embl.mobie.command.open.OpenTableCommand;
import org.embl.mobie.lib.transform.GridType;

import java.io.File;

public class OpenTeresaData
{
    public static void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

//        OpenTableCommand command = new OpenTableCommand();
//        command.table = new File( "/Users/tischer/Desktop/teresa/summary_calculated1_subset.txt" );
//        command.root = command.table.getParentFile();
//        command.images = "FileName_Result.Image_IMG"; // Result.Image.Zarr
//        command.gridType = GridType.Transformed; // TODO: not working with Stitched!
//        command.run();

        OpenTableCommand command = new OpenTableCommand();
        command.table = new File( "/Volumes/CRISPR_project_data/test_data_new/20231122-170451/summary_new.txt" );
        command.root = command.table.getParentFile();
        command.images = "FileName_Result.Image_IMG";
        command.gridType = GridType.Transformed; // TODO: not working with Stitched!
        command.run();

        /*
        TODO:

        Why is it fetching so much metadata?? It is opening the same data again for all channels.

        Also initialisation is slow...

        Min, max: [6.0, 2608.0]
        Fetching metadata from /Volumes/CRISPR_project_data/test_data_new/20231122-170451/Result-Image/Result-Image--WA01--P0001--T0001.oir
        Slices: 11
        Frames: 1
        Min, max: [13.0, 981.0]
        Fetching metadata from /Volumes/CRISPR_project_data/test_data_new/20231122-170451/Result-Image/Result-Image--WA01--P0001--T0001.oir
        Slices: 11
        Frames: 1
        Min, max: [7.0, 2060.0]
         */

    }
}
