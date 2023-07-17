package com.deepoove.authsaur.configurer.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ServiceGroup implements Comparable {
    private String name;
    private List<ServiceVO> services = new ArrayList<>();

    @Override
    public int compareTo(Object o) {
        if (this.name == null) return 1;
        if (o == null) return -1;
        return new CompareToBuilder()
                .append(this.getName(), ((ServiceGroup) o).getName())
                .toComparison();
    }
}
