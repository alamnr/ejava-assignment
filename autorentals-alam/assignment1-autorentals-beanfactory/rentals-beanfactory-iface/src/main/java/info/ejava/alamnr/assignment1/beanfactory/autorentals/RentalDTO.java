package info.ejava.alamnr.assignment1.beanfactory.autorentals;


public class RentalDTO {

    private String name;

    public RentalDTO(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return "{ " + name + " }";
    }
    
}
