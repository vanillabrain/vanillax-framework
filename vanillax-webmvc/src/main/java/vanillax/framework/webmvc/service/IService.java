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
 * Rest를 지원하기 위한 서비스 스펙
 */
public interface IService {
	@SuppressWarnings("rawtypes")
	public void service(Map param);
	public Object find(Object obj);
	public Object findOne(Object obj);
	public Object findMany(Object obj);
	public Object insert(Object obj);
	public Object update(Object obj);
	public Object delete(Object obj);
	public void setId(String id);
	public String getId();
}
