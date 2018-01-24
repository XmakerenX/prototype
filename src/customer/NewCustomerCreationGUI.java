package customer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import client.Client;
import client.ClientInterface;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import networkGUI.StoreManagerGUI;
import prototype.FormController;
import serverAPI.Response;
import user.UserController;

/**
 * A GUI for store manager to create new customer in his/her store. 
 */
public class NewCustomerCreationGUI extends FormController implements ClientInterface{
	
	// holds the last replay we got from server
	private Response replay = null;
	
	//List of payment methods for the combo box
	ObservableList<String> paymentMethodList = FXCollections.observableArrayList();
	
	// Store manager's store ID
	private long managersStoreID;

    @FXML // fx:id="personIDTxtField"
    private TextField personIDTxtField;

    @FXML // fx:id="windowTitleLbl"
    private Label windowTitleLbl;

    @FXML // fx:id="lastNameLbl"
    private Label lastNameLbl;

    @FXML // fx:id="creditCardNumberLbl"
    private Label creditCardNumberLbl;

    @FXML // fx:id="createCustomerBtn"
    private Button createCustomerBtn;

    @FXML // fx:id="lastNameTxtField"
    private TextField lastNameTxtField;

    @FXML // fx:id="phoneNumberLbl"
    private Label phoneNumberLbl;

    @FXML // fx:id="firstNameLbl"
    private Label firstNameLbl;

    @FXML // fx:id="personIDLbl"
    private Label personIDLbl;

    @FXML // fx:id="firstNameTxtField"
    private TextField firstNameTxtField;

    @FXML // fx:id="backBtn"
    private Button backBtn;

    @FXML // fx:id="instructionLbl"
    private Label instructionLbl;

    @FXML // fx:id="paymentMethodLbl"
    private Label paymentMethodLbl;

    @FXML // fx:id="phoneNumberTxtField"
    private TextField phoneNumberTxtField;

    @FXML // fx:id="paymentMethodComboBox"
    private ComboBox<String> paymentMethodComboBox;

    @FXML // fx:id="creditCardNumberTxtField"
    private TextField creditCardNumberTxtField; 

    /**
     * Initializes the combo box of payment method.
     */
    @FXML
    //Will be called by FXMLLoader
    public void initialize(){
    	
		String temporaryString = "";
    	ArrayList<String> paymentMethods = new ArrayList<String>();
    	
    	for(Customer.PayType paytype : Customer.PayType.values())
    	{
    		temporaryString = handleSplittedStringFromDataBase(""+paytype);
    		paymentMethods.add(""+temporaryString);
    	}
    	
    	paymentMethodList.addAll(paymentMethods);
    	paymentMethodComboBox.setItems(paymentMethodList);
    	
    	setListenersForTextFields();
    }
    
    /**
     * Creates new customer and adds him/her to data base
     * @param event - "Create customer" button is pressed
     */
    @FXML
    void onCreateCustomer(ActionEvent event) {
    	
		//User must fill all fields, otherwise new customer cannot be created
    	if(!personIDTxtField.getText().equals("") && !firstNameTxtField.getText().equals("")
				&& !lastNameTxtField.getText().equals("") && !phoneNumberTxtField.getText().equals("")
				&& paymentMethodComboBox.getValue() != null && !creditCardNumberTxtField.getText().equals(""))
    	{
	    	String personID = personIDTxtField.getText();
	    	
	    	CustomerController.getCustomer(personID, ""+managersStoreID, client);
	    	
	     	try
	    	{
	    		synchronized(this)
	    		{
	    			// wait for server response
	    			this.wait();
	    		}
	    	
	    		if (replay == null)
	    			return;
	    		
	    	// show success 
	    	if (replay.getType() == Response.Type.SUCCESS)
	    		showWarningMessage("Customer already exists in this store");
	
	    	else
	    	{
	    		// clear replay
	    		replay = null;
	    		
	    		//Attains info from the GUI and sends request to server to create new customer

	    		String firstName;
	    		String lastName;
	    		String fullName;
	    		String phoneNumber;
	    		String paymentMethod;
	    		String creditCardNumber;
	    		Calendar expirationDate = Calendar.getInstance();
	    		Date date = null;
	    		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
	    		
	    		firstName = firstNameTxtField.getText();
	    		lastName = lastNameTxtField.getText();
	    		fullName = firstName + " " + lastName;
	    		phoneNumber = phoneNumberTxtField.getText();
	    		paymentMethod = handleSplittedStringFromGUI(paymentMethodComboBox.getValue());
	    		creditCardNumber = creditCardNumberTxtField.getText();

	    		if(paymentMethod.equals("MONTHLY_SUBSCRIPTION"))
	    		{
	    			expirationDate.add(Calendar.MONTH, 1);
	    		}
	    		else if(paymentMethod.equals("YEARLY_SUBSCRIPTION"))
	    		{
	    			expirationDate.add(Calendar.YEAR, 1);
	    		}
	    		else
	    			expirationDate = null;
	    		
	    		if(expirationDate != null)
	    			date = expirationDate.getTime();
	    		

	    		CustomerController.createNewCustomer(Long.parseLong(personID), managersStoreID, fullName, phoneNumber,
	    				Customer.PayType.valueOf(paymentMethod), 0, creditCardNumber, true, expirationDate, client);
	    		
	    		try
		    	{
		    		synchronized(this)
		    		{
		    			// wait for server response
		    			this.wait();
		    		}
		    	
		    		if (replay == null)
		    			return;
		    		
		    	// show success 
		    	if (replay.getType() == Response.Type.SUCCESS)
		    		showInformationMessage("New customer has been created!");
		    	else
		    		showErrorMessage("Failed to send data to server!");
		    	
		    	}catch(InterruptedException e) {}
	    		
	    	}
	    	
	    	}catch(InterruptedException e) {}
    	}
    	else
    		showWarningMessage("All fields must be filled!");
    }
    
    /**
     * Returns to previous GUI window 
     * @param event - "Back" button is pressed
     */
    @FXML
    void onBack(ActionEvent event) {
    	
    	personIDTxtField.setText("");
    	firstNameTxtField.setText("");
		lastNameTxtField.setText("");
		phoneNumberTxtField.setText("");
		paymentMethodComboBox.setValue(null);
		creditCardNumberTxtField.setText("");
    	
    	StoreManagerGUI storeManagerGUI = (StoreManagerGUI)parent;
    	client.setUI(storeManagerGUI);
    	FormController.primaryStage.setScene(parent.getScene());
    }



    /**
     * Displays reply message from the server
     */
	@Override
	public void display(Object message) {
    	System.out.println(message.toString());
    	System.out.println(message.getClass().toString());
		
		Response replay = (Response)message;
		this.replay = replay;
		
		synchronized(this)
		{
			this.notify();
		}
	}

	@Override
	public void onSwitch(Client newClient) {
		
	}
	
	/**
	 * Receives message that will be splitted by "_" symbol and transformed to user friendly view.
	 * For example: "CREDIT_CARD" is transformed to "Credit card"
	 * @param stringToSplit - message to be splitted by specific symbol
	 * @return transformed string
	 */
	
	public String handleSplittedStringFromDataBase(String stringToSplit)
	{
		String [] splittedString;
		splittedString = stringToSplit.split("_");
		
		String tempString = "";
		
		for(String splitted : splittedString)
		{
			splitted = splitted.toLowerCase();
			
			if(!tempString.equals(""))
				tempString = tempString + " ";
			else
				splitted = Character.toUpperCase(splitted.charAt(0)) + splitted.substring(1);
			
			tempString = tempString + splitted;
		}
		
		return tempString;
	}
	
	/**
	 * Receives message that will be splitted by " " symbol and transformed to data base view.
	 * For example: "Credit card" is transformed to "CREDIT_CARD"
	 * @param stringToSplit - message to be splitted by specific symbol
	 * @return transformed string
	 */
	
	public String handleSplittedStringFromGUI(String stringToSplit)
	{
		String [] splittedString;
		splittedString = stringToSplit.split(" ");
		
		String tempString = "";
		
		for(String splitted : splittedString )
			{
		   		if( !tempString.equals(""))
		   			tempString = tempString + "_";
		   		tempString = tempString + splitted.toUpperCase();
		   	}
		
		return tempString;
	}
	
	private void setListenersForTextFields()
	{
    	personIDTxtField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("([0-9]*)") && newValue.length() <= 9) 
                {
                	personIDTxtField.setText(newValue);
                }
                else
                	personIDTxtField.setText(oldValue);
            }
        });
    	
    	phoneNumberTxtField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("([0-9]*)") && newValue.length() <= 16) 
                {
                	phoneNumberTxtField.setText(newValue);
                }
                else
                	phoneNumberTxtField.setText(oldValue);
            }
        });

    	creditCardNumberTxtField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("([0-9]*)") && newValue.length() <= 16) 
                {
                	creditCardNumberTxtField.setText(newValue);
                }
                else
                	creditCardNumberTxtField.setText(oldValue);
            }
        });
    	
    	firstNameTxtField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("([a-zA-Z]*)") && newValue.length() <= 30) 
                {
                	firstNameTxtField.setText(newValue);
                }
                else
                	firstNameTxtField.setText(oldValue);
            }
        });
    	
    	lastNameTxtField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("([a-zA-Z]*)") && newValue.length() <= 30) 
                {
                	lastNameTxtField.setText(newValue);
                }
                else
                	lastNameTxtField.setText(oldValue);
            }
        });

	}
	
	public void setManagersStoreID(long managersStoreID) {
		this.managersStoreID = managersStoreID;
	}

}
