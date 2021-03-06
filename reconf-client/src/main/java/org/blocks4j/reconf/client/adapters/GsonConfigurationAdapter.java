/*
 *   Copyright 2013-2015 Blocks4J Team (www.blocks4j.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.blocks4j.reconf.client.adapters;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.blocks4j.reconf.client.constructors.MethodData;

public class GsonConfigurationAdapter implements ConfigurationAdapter<Object> {

    private Gson mapper = new GsonBuilder().create();

    @Override
    public Object adapt(MethodData methodData) {
        return this.mapper.fromJson(methodData.getValue(), methodData.getReturnType());
    }
}
