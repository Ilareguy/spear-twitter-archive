package com.ilareguy.spear.data_fetcher;

import com.ilareguy.spear.PageAbstract;

/**
 * Like DataFetcherAbstract, but all requests of this type are remote/oauth requests;
 * therefore you don't need to override onCache().
 * @param <D> The type of data that will be returned.
 */
public abstract class RemoteDataFetcherAbstract<D> extends DataFetcherAbstract<D>{

    public RemoteDataFetcherAbstract(final PageAbstract page) {
        super(page, Policy.FORCE_REMOTE);
    }

    @Override
    protected final Result<D> onCache(){ return null; }

}
