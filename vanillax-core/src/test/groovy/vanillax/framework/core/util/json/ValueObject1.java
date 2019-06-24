package vanillax.framework.core.util.json;

import java.beans.Transient;

public class ValueObject1 {
    private String s1 = null;
    private String s2 = null;
    private String s3 = null;
    private String s4 = null;
    transient private String s5 = null;

    public ValueObject1(String s1, String s2, String s3, String s4, String s5) {
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
        this.s5 = s5;
    }

    public void setS1(String s1) {
        this.s1 = s1;
    }

    @Transient
    public String getS2() {
        return s2;
    }

    public void setS2(String s2) {
        this.s2 = s2;
    }

    public String getS3() {
        return s3;
    }

    public void setS3(String s3) {
        this.s3 = s3;
    }

    public String getS4() {
        return s4;
    }

    public void setS4(String s4) {
        this.s4 = s4;
    }

    public String getS5() {
        return s5;
    }

    public void setS5(String s5) {
        this.s5 = s5;
    }

    public String getString6() {
        return s5;
    }
}
