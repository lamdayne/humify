package com.lamdayne.humify.common.base;

public interface BaseAccessService<T, ID> {

    T getReferenceById(ID id);

    T getById(ID id);

    boolean existsById(ID id);

}
