package com.deepoove.authsaur.result;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
public class Page<T> {

    private List<T> list;
    private int total;
    private int pageSize;
    private int current;

    public Page() {
    }

    public Page(int current, int pageSize, int total) {
        this.current = current;
        this.pageSize = pageSize;
        this.total = total;
    }

    public static int wrapPageSize(Integer pageSize) {
        if (null == pageSize) return 10;
        if (pageSize >= 1000) {
            LOGGER.warn("Maximum paging limit 1000");
            return 1000;
        }
        return pageSize.intValue();
    }

    public static int wrapPageCurrent(Integer current) {
        if (null == current) return 1;
        return current.intValue();
    }

    public <N> Page<N> toNew(List<N> newList) {
        Page<N> nPage = new Page<>(current, pageSize, total);
        nPage.list = newList;
        return nPage;
    }
}
