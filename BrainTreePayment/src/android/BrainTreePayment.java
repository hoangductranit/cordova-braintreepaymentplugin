package cordova.plugin.braintreepaymentplugin.braintree;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.braintreepayments.api;
import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.GooglePayment;
import com.braintreepayments.api.models.GooglePaymentRequest;
import com.braintreepayments.api.GooglePaymentActivity;
import com.braintreepayments.api.models.GooglePaymentCardNonce;
import com.braintreepayments.api.models.PaymentMethodNonceFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class BrainTreePayment extends CordovaPlugin {

    private GooglePayment googlePayment=null;
    private BraintreeFragment braintreeFragment=null;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    private synchronized void checkGooglePaySupported(final CallbackContext callbackContext) throws JSONException {
        try
        {
                if(braintreeFragment!=null)
                {
                    GooglePayment.isReadyToPay(braintreeFragment, new BraintreeResponseListener<Boolean>() {
                        @Override
                        public void onResponse(Boolean isReadyToPay) {
                            if (isReadyToPay) {
                                callbackContext.success();
                            }
                            else
                            {
                                callbackContext.error("Google Pay is not supported on this device");
                                return;
                            }
                        }
                    });
                }
                else
                {
                    callbackContext.error("Google Pay is not supported on this device");
                    return;
                }
        }
        catch (Exception exception) {
            callbackContext.error("Google Pay is not supported on this device");
            return;
        }
        callbackContext.success();
    }
    
    private synchronized void initialize(final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        // Ensure we have the correct number of arguments.
        if (args.length() != 1) {
            callbackContext.error("A token is required.");
            return;
        }

        // Obtain the arguments.
        String token = args.getString(0);

        if (token == null || token.equals("")) {
            callbackContext.error("A token is required.");
            return;
        }
        braintreeFragment = BraintreeFragment.newInstance((AppCompatActivity)this.cordova.getActivity(), token);

        if (braintreeFragment == null) {
            callbackContext.error("The Braintree client failed to initialize.");
            return;
        }

        callbackContext.success();
    }

    private synchronized void sendRequest(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (args.length() != 1) {
            callbackContext.error("merchant id requires.");
            return;
        }
        String merchantId=args.getString(0);

        if(merchantId==null||merchantId.equals(""))
        {
            callbackContext.error("merchant id requires.");
            return;
        }
        GooglePaymentRequest googlePaymentRequest = new GooglePaymentRequest()
        .transactionInfo(TransactionInfo.newBuilder()
        .setTotalPrice("1.00")
        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
        .setCurrencyCode("USD")
        .build())
        // We recommend collecting billing address information, at minimum
        // billing postal code, and passing that billing postal code with all
        // Google Pay card transactions as a best practice.
        .billingAddressRequired(true)
        // Optional in sandbox; if set in sandbox, this value must be a valid production Google Merchant ID
        .googleMerchantId(merchantId);
        
        this.cordova.setActivityResultCallback(this);
        this.cordova.startActivityForResult(this, paymentRequest.getIntent(this.cordova.getActivity()), DROP_IN_REQUEST);
    }  
}
