/*
 * Copyright (C) 2016 Vanilla Brain, Team - All Rights Reserved
 *
 * This file is part of 'VanillaTopic'
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Vanilla Brain Team and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Vanilla Brain Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vanilla Brain Team.
 */

package vanillax.framework.webmvc.service;

import java.util.Map;

/**
 * Created by gaedong on 8/5/15.
 */
public class ServiceBase implements IService {
    protected String id = null;

    @Override
    public void service(Map param) {
    }

    @Override
    public Object findOne(Object obj) {
        return null;
    }

    @Override
    public Object findMany(Object obj) {
        return null;
    }

    @Override
    public Object find(Object obj) {
        return get(obj);
    }

    public Object get(Object obj) {
        return null;
    }

    @Override
    public Object insert(Object obj) {
        return post(obj);
    }

    public Object post(Object obj) {
        return null;
    }

    @Override
    public Object update(Object obj) {
        return put(obj);
    }

    public Object put(Object obj) {
        return null;
    }

    @Override
    public Object delete(Object obj) {
        return null;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }


}
