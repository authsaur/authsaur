package com.deepoove.authsaur.zentao;

import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ZentaoServiceRegistry extends ImmutableInMemoryServiceRegistry {
    public ZentaoServiceRegistry(final List<RegisteredService> services,
                                 final ConfigurableApplicationContext applicationContext) {
        super(services, applicationContext, new ArrayList<>(0));
    }

    public ZentaoServiceRegistry(final ConfigurableApplicationContext applicationContext, final RegisteredService... services) {
        this(Arrays.stream(services).collect(Collectors.toList()), applicationContext);
    }

}