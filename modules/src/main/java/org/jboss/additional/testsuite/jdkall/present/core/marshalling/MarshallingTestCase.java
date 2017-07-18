/*
 * JBoss, Home of Professional Open Source.
 * Copyleft 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.additional.testsuite.jdkall.present.core.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.eap.additional.testsuite.annotations.EapAdditionalTestsuite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import org.jboss.marshalling.InputStreamByteInput;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.OutputStreamByteOutput;
import org.jboss.marshalling.river.RiverMarshaller;
import org.jboss.marshalling.river.RiverMarshallerFactory;
import org.jboss.marshalling.river.RiverUnmarshaller;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@RunWith(Arquillian.class)
@EapAdditionalTestsuite({"modules/testcases/jdkAll/Wildfly/core/src/main/java","modules/testcases/jdkAll/Eap7/core/src/main/java"})
public class MarshallingTestCase {

    public static final String DEPLOYMENT = "marshallingTestCase.war";

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addClass(Foo.class);
        archive.addClass(Bar.class);
        archive.addPackage("org.jboss.marshalling");
        archive.addPackage("org.jboss.marshalling.util");
        archive.addPackage("org.jboss.marshalling.river");
        archive.addPackage("org.jboss.marshalling.reflect");
        return archive;
    }

    @Test
    public void deserializationTest(@ArquillianResource URL url) throws Exception {
        RiverMarshallerFactory factory = new RiverMarshallerFactory();  
	    MarshallingConfiguration configuration = new MarshallingConfiguration();  
	  
	    configuration.setVersion(2); 
	  
	    // Create a marshaller on some stream we have  
	    RiverMarshaller marshaller = (RiverMarshaller) factory.createMarshaller(configuration);  
            final ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream();
	    marshaller.start(new OutputStreamByteOutput(fileOutputStream));  
	  
            Bar bar = new Bar("Hello");
            Foo foo = new Foo(bar);
	    // Write lots of stuff  
	    marshaller.writeObject(foo);  
          
	    // Done  
	    marshaller.finish();  
            
            RiverUnmarshaller unmarshaller = (RiverUnmarshaller) factory.createUnmarshaller(configuration);  
            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileOutputStream.toByteArray());
            unmarshaller.start(new InputStreamByteInput(fileInputStream));  
            
            try {
                Foo f = unmarshaller.readObject(Foo.class);
                Assert.assertEquals(f.bar.aString,"Hello");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            unmarshaller.finish();
    }
}