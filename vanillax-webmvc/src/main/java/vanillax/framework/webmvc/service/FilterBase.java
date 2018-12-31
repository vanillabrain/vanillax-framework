package vanillax.framework.webmvc.service;

import java.util.Map;

public class FilterBase implements IFilter{
    @Override
    public Map<String, Object> preprocess(Map<String, Object> param) throws Exception {
        return param;
    }

    @Override
    public Object postprocess(Object result) throws Exception {
        return result;
    }
}
