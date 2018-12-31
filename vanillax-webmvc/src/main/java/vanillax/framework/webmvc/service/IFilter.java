package vanillax.framework.webmvc.service;

import java.util.Map;

public interface IFilter {
    public Map<String, Object> preprocess(Map<String, Object> param) throws Exception;
    public Object postprocess(Object result) throws Exception;
}
