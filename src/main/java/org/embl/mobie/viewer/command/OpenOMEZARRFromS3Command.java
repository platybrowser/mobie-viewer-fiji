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
package org.embl.mobie.viewer.command;

import ij.gui.OvalRoi;
import ij.process.ImageProcessor;
import org.embl.mobie.OMEZarrViewer;
import mpicbg.spim.data.SpimData;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.IOException;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataViewer>OME-Zarr>Open OME-Zarr From S3...")
public class OpenOMEZARRFromS3Command implements Command {

    @Parameter(label = "S3 URL")
    public String s3URL = "https://s3.embl.de/i2k-2020/platy-raw.ome.zarr";

    @Parameter ( label = "Log chunk loading" )
    public boolean logChunkLoading = false;

    protected static void openAndShow(String s3URL) throws IOException {
        s3URL = s3URL.replaceAll( "\\s", "" ); // TODO: Maybe add general function and/or put deeper into code: https://github.com/mobie/mobie-viewer-fiji/issues/654
        SpimData spimData = OMEZarrS3Opener.readURL(s3URL.trim());
        final OMEZarrViewer viewer = new OMEZarrViewer(spimData);
        viewer.show();
    }

    @Override
    public void run() {
        try {
            OMEZarrS3Opener.setLogging( logChunkLoading );
            openAndShow(s3URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
