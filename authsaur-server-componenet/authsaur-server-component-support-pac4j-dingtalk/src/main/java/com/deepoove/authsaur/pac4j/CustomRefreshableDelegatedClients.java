package com.deepoove.authsaur.pac4j;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.RefreshableDelegatedClients;
import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;
import org.pac4j.core.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class CustomRefreshableDelegatedClients extends RefreshableDelegatedClients {

    @Autowired
    CasConfigurationProperties casProperties;

    private List<Client> operClients = new ArrayList<>();

    public CustomRefreshableDelegatedClients(String callbackUrl,
                                             DelegatedClientFactory<Client> delegatedClientFactory) {
        super(callbackUrl, delegatedClientFactory);
    }

    protected List<Client> buildDelegatedClients() {
        val clients = super.buildDelegatedClients();
//        configureDingTalkClient(clients);
//        configureDingTalklessClient(clients);
        // configureWeiboClient(clients);
        clients.addAll(operClients);
        return new ArrayList<>(clients);
    }


    public void addClient(Client client) {
        operClients.add(client);
    }

    public void removeClient(String name) {
        operClients.removeIf(c -> c.getName().equals(name));
    }

    public void clearClient() {
        operClients.clear();
    }

}
