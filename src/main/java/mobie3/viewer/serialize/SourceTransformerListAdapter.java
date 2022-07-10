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
package mobie3.viewer.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import mobie3.viewer.transform.AffineTransformation;
import mobie3.viewer.transform.CropImageTransformation;
import mobie3.viewer.transform.MergedGridImageTransformation;
import mobie3.viewer.transform.Transformation;
import mobie3.viewer.transform.TimepointsImageTransformation;
import mobie3.viewer.transform.TransformedGridImageTransformation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SourceTransformerListAdapter implements JsonSerializer< List< Transformation > >, JsonDeserializer< List< Transformation > >
{
	private static Map<String, Class> nameToClass = new TreeMap<>();
	private static Map<String, String> classToName = new TreeMap<>();

	static {
		nameToClass.put("mergedGrid", MergedGridImageTransformation.class);
		classToName.put( MergedGridImageTransformation.class.getName(), "mergedGrid");
		nameToClass.put("transformedGrid", TransformedGridImageTransformation.class);
		classToName.put( TransformedGridImageTransformation.class.getName(), "transformedGrid");
		nameToClass.put("affine", AffineTransformation.class);
		classToName.put( AffineTransformation.class.getName(), "affine");
		nameToClass.put("timepoints", TimepointsImageTransformation.class);
		classToName.put( TimepointsImageTransformation.class.getName(), "timepoints");
		nameToClass.put("crop", CropImageTransformation.class);
		classToName.put( CropImageTransformation.class.getName(), "crop");
	}

	@Override
	public List< Transformation > deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		List list = new ArrayList< Transformation >();
		JsonArray ja = json.getAsJsonArray();

		for ( JsonElement je : ja )
		{
			list.add( JsonHelper.createObjectFromJsonValue( context, je, nameToClass ) );
		}

		return list;
	}

	@Override
	public JsonElement serialize( List< Transformation > imageTransformations, Type type, JsonSerializationContext context ) {
		JsonArray ja = new JsonArray();
		for ( Transformation imageTransformation : imageTransformations ) {
			Map< String, Transformation > nameToTransformer = new HashMap<>();
			nameToTransformer.put( classToName.get( imageTransformation.getClass().getName() ), imageTransformation );

			if ( imageTransformation instanceof TransformedGridImageTransformation ) {
				ja.add( context.serialize( nameToTransformer, new TypeToken< Map< String, TransformedGridImageTransformation > >() {}.getType() ) );
			} else if ( imageTransformation instanceof AffineTransformation ) {
				ja.add( context.serialize( nameToTransformer , new TypeToken< Map< String, AffineTransformation > >() {}.getType() ) );
			} else if ( imageTransformation instanceof CropImageTransformation ) {
				ja.add( context.serialize( nameToTransformer , new TypeToken< Map< String, CropImageTransformation > >() {}.getType() ) );
			} else if ( imageTransformation instanceof MergedGridImageTransformation ) {
				ja.add( context.serialize( nameToTransformer, new TypeToken< Map< String, MergedGridImageTransformation > >() {}.getType() ) );
			} else if ( imageTransformation instanceof TimepointsImageTransformation ) {
				ja.add( context.serialize( nameToTransformer, new TypeToken< Map< String, TimepointsImageTransformation > >(){}.getType()) );
			} else {
				throw new UnsupportedOperationException( "Could not serialise SourceTransformer of type: " + imageTransformation.getClass().toString() );
			}
		}

		return ja;
	}
}
