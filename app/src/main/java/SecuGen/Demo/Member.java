package SecuGen.Demo;

/**
 * Created by SHAKTHI on 15/02/2020.
 */

public class Member {
    String nameStr;
    float amtFloat;
    String template;


    public  String getTemplate() {return template;}

    public void setTemplate(String template){ this.template = template; }

    public String getNameStr() {
        return nameStr;
    }

    public void setNameStr(String nameStr) {
        this.nameStr = nameStr;
    }

    public float getAmtFloat() {
        return amtFloat;
    }

    public void setAmtFloat(float amtFloat) {
        this.amtFloat = amtFloat;
    }
}
