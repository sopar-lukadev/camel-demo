package com.endava.demo.connector.config;

import org.apache.camel.CamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.camel.spi.RestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfiguration {

    private ServerConfiguration serverConfiguration;

    @Autowired
    public void configure(CamelContext context, ServerConfiguration serverConfiguration) throws Exception {
        this.serverConfiguration = serverConfiguration;
        context.setRestConfiguration(createRestConfiguration(context));

        // value <=0 disables flushing to disk completely
        context.getStreamCachingStrategy().setSpoolThreshold(-1L);
    }

    private RestConfiguration createRestConfiguration(CamelContext camelContext) throws Exception {
        RestConfiguration restConfiguration = new RestConfiguration();
        return new RestConfigurationDefinition()
                .component("jetty")
                .scheme(serverConfiguration.getScheme())
                .host(serverConfiguration.getHost())
                .port(serverConfiguration.getPort())
                .bindingMode(RestBindingMode.off)
                .asRestConfiguration(camelContext, restConfiguration);
    }
}
