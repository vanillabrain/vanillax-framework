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

package vanillax.framework.batch.action;

import java.util.UUID;

/**
 * Created by gaedong on 2015-08-13.
 */
public class ActionBase implements IAction {
    protected String actionId = null;
    protected String id = null;

    @Override
    public Object process(Object object)throws Exception {
        this.actionId = UUID.randomUUID().toString();
        return object;
    }

    @Override
    public void clear() {
        //do nothing
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getActionId() {
        return this.actionId;
    }
}
