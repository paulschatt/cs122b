function calculateTotalPrice(resultData){
    totalPrice = 0;
    for (let i = 0; i < resultData.length; i++){
        totalPrice = totalPrice + Number(resultData[i]["price"])*Number(resultData[i]["quantity"]);totalPrice + Number(resultData[i]["price"])*Number(resultData[i]["quantity"]);
    }
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart", // Setting request url, which is mapped by HomeServlet in HomeServlet.java
    success: (resultData) => {
        calculateTotalPrice(resultData);
        $('#total-price').text('Total Price: $'+totalPrice);
    }
});

jQuery(document).on('click', '.submit', function(event) {
    event.preventDefault();
    console.log("Submit button clicked"); // Check if the event is triggered
    const fname = $('#fname').prop('value');
    const lname = $('#lname').prop('value');
    const creditcard = $('#cardnumber').prop('value');
    const date = $('#date').prop('value');
    jQuery.ajax({
        dataType:"json",
        method:"POST",
        url:"api/payment-processing",
        data:{
            fname:fname,
            lname:lname,
            creditcard:creditcard,
            date:date,
        },
        success: (resultData) => {
            alert("Successful Purchase!");
            window.location.replace("index.html")
        },
        error: function(xhr, status, error) {
            alert("Oops! Something went wrong - please try again.")
            console.log("Error removing movie from cart");
        }
    })
});