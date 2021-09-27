package example;

import org.springframework.stereotype.Repository;

@Repository
public class OrderDao {

	public String toUpperCase(String input){
		return "This is the UppercaseString : "+input;
	}
}
