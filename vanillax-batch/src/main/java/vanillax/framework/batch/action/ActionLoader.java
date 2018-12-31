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

import vanillax.framework.core.object.ObjectLoader;

/**
 * action groovy 파일을 읽어서 Java Instance로 만들어 Cache에 저장한다.
 * 변경여부를 확인하여 변경되었을 경우 다시로딩한다.
 */
public class ActionLoader {

    /**
     * Service 객체를 로딩한다.
     * @param path 예) "my.package.Hello"
     * @return
     * @throws Exception
     */
    public static IAction load(String path)throws Exception {
        return (IAction) ObjectLoader.load(path);
    }

}
