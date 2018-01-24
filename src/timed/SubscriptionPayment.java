package timed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import Server.DBConnector;

public class SubscriptionPayment extends TimerTask
{
	DBConnector conn = null;
	String year;
	public SubscriptionPayment(String year, DBConnector db)
	{
		this.conn = db;
		this.year = year;
	}
	
	@Override
	public void run() {
		Calendar cal = Calendar.getInstance();
		   SimpleDateFormat formatter = new SimpleDateFormat("dd");
		   String date = formatter.format(cal.getTime());
		   //Checks if the date is right for creating new reports
		   if(date.equals("01") || date.equals(formatter.format(cal.getTime())))		//checks whether the it's the first of a month 
		   {
			   formatter = new SimpleDateFormat("MM");
			   String monthOfPayment = formatter.format(cal.getTime());
			   if(monthOfPayment.equals("01"))		//if we are at the beginning of the year
			   {
				   System.out.println("it's January now!");
				   year="" + (Integer.valueOf(year)-1);
				   System.out.println("Year for payment is: "+year);
			   }
			   cal = Calendar.getInstance();
			   formatter = new SimpleDateFormat("MM");
			   String stringMonth = formatter.format(cal.getTime());
			   int month = Integer.valueOf(formatter.format(cal.getTime()))-1;
			   System.out.println("It's time to PAY! MUHAHA");
			   
			   SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			   
			   Calendar from = Calendar.getInstance();
			   from.add(Calendar.MONTH, -1);
			   from.set(Calendar.MINUTE, 00);
			   from.set(Calendar.HOUR_OF_DAY, 00);
			   from.set(Calendar.SECOND, 00);
			   from.set(Calendar.DAY_OF_MONTH, 01);
			   from.set(Calendar.YEAR, Integer.valueOf(year));
			   
			   Calendar to = Calendar.getInstance();
			   to.add(Calendar.MONTH, -1);
			   to.set(Calendar.MINUTE, 59);
			   to.set(Calendar.HOUR_OF_DAY, 23);
			   to.set(Calendar.SECOND, 59);
			   to.set(Calendar.DAY_OF_MONTH, 31);
			   to.set(Calendar.YEAR, Integer.valueOf(year));
			   ArrayList<Integer> customerIDs = new ArrayList<Integer>();
			   ArrayList<Float> spendings = new ArrayList<Float>();
			   //all the customers who have bought anything during this month
			   ResultSet rs = conn.selectTableData("OrderCustomerID", "order", "OrderPaymentMethod = 'SUBSCRIPTION' AND "
				   		+ "STR_TO_DATE(OrderCreationDateTime, '%Y-%m-%d %k:%i:%s') between '"+format.format(from.getTime())+"' and '"+format.format(to.getTime())+"' ");
			   try
				  {
					  while (rs.next())
					  {
						  if(!customerIDs.contains(rs.getInt("OrderCustomerID")))
						  {
							  customerIDs.add(rs.getInt("OrderCustomerID"));
						  }
						  
					  }
				  }catch (SQLException e) {e.printStackTrace();}
			   
			   System.out.println(customerIDs);
			   int customerNumber = customerIDs.size();
			   System.out.println("we have "+ customerNumber + " customers.");
			   
			   //for each customer who used subscription, we are updating how much they have spent 
			   if(customerNumber!=0) {
				   for(int i=0; i<customerNumber; i++)
				   {
					   spendings.add((float) 0);
					   rs = conn.selectTableData("*", "order", "OrderCustomerID = "+customerIDs.get(i)+ " AND OrderPaymentMethod = 'SUBSCRIPTION' AND "
						   		+ "STR_TO_DATE(OrderCreationDateTime, '%Y-%m-%d %k:%i:%s') between '"+format.format(from.getTime())+"' and '"+format.format(to.getTime())+"' ");
					   try
						  {
							  while (rs.next())
							  {
								  spendings.set(i, spendings.get(i) + rs.getFloat("OrderPrice"));
								  
							  }
						  }catch (SQLException e) {e.printStackTrace();}
					   System.out.println(spendings);
				   }
			   }
			   
			   //now customerIDs(i) holds the id
			   //and spendings(i) holds that customer's spendings
			   
		   }
		
	}

}
