/**
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
let totalPrice = 0;

function calculateTotalPrice(resultData){
    totalPrice = 0;
    for (let i = 0; i < resultData.length; i++){
        totalPrice = totalPrice + Number(resultData[i]["price"])*Number(resultData[i]["quantity"]);totalPrice + Number(resultData[i]["price"])*Number(resultData[i]["quantity"]);
    }
}

function handleShoppingCartResult(resultData){
    console.log("handleShoppingCartResult: populating shopping cart table from resultData");
    console.log(resultData);

    let shoppingCartTableBodyElement = jQuery("#shopping_cart_table_body");
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["title"] + "</td>";
        rowHTML += "<td>" + resultData[i]["price"] + "</td>";
        rowHTML += "<td>";
        rowHTML += "<a class = 'quantity'>" + resultData[i]["quantity"] + "</a>";
        rowHTML += "</td>";
        rowHTML += "<td>";
        rowHTML += "<button type='button' class='btn btn-dark decrease-quantity' data-movie-id='" + resultData[i]['movieId'] + "' data-quantity='" + resultData[i]['quantity'] + "'>-</button>";
        rowHTML += "<button type='button' class='btn btn-dark increase-quantity' data-movie-id='" + resultData[i]['movieId'] + "' data-quantity='" + resultData[i]['quantity'] + "'>+</button>";
        rowHTML += "</td>";
        rowHTML += "</td>";
        rowHTML += "<td><button type ='button' class='btn btn-dark remove-from-cart' data-movie-id='" + resultData[i]['movieId'] + "'>Remove from Cart</button></td>";
        rowHTML += "</tr>";
        shoppingCartTableBodyElement.append(rowHTML);
    }
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart", // Setting request url, which is mapped by HomeServlet in HomeServlet.java
    success: (resultData) => {
        calculateTotalPrice(resultData);
        $('#total-price').text('Total Price: $'+totalPrice);
        handleShoppingCartResult(resultData)
    } // Setting callback function to handle data returned successfully by the HomeServlet
});

jQuery(document).on('click', '.remove-from-cart', function(event) {
    // Get the movie ID from the clicked button
    let movieId = jQuery(this).data('movie-id');
    // Make an AJAX POST request to add the movie to the cart
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            action:"remove",
            movieId: movieId,
        },
        success: resultData => {
            console.log("Removed movie from cart");
            $(this).parents('tr').remove();
            calculateTotalPrice(resultData);
            $('#total-price').text('Total Price: $'+totalPrice);
        },
        error: function(xhr, status, error) {
            // Handle the error if the request fails
            console.log("Error removing movie from cart:" + error);
        }
    });
});

jQuery(document).on('click', '.decrease-quantity', function (event){
    let movieId = jQuery(this).data('movie-id');
    let quantity = jQuery(this).data('quantity')
    if(quantity > 1) {
        quantity = quantity-1;
        jQuery.ajax({
            dataType: "json",
            method: "POST",
            url: "api/cart",
            data: {
                action: "update",
                movieId: movieId,
                quantity: quantity,
            },
            success: resultData => {
                console.log("Decreased movie quantity");
                $(this).parents('tr').find('a').text(quantity);
                jQuery(this).data('quantity', quantity);
                jQuery(this).siblings('.increase-quantity').data('quantity', quantity);
                calculateTotalPrice(resultData);
                $('#total-price').text('Total Price: $'+totalPrice);
            },
            error: function (xhr, status, error) {
                // Handle the error if the request fails
                console.log("Error decreasimg movie quantity:" + error);
            }
        });
    }
});

jQuery(document).on('click', '.increase-quantity', function (event){
    let movieId = jQuery(this).data('movie-id');
    let quantity = jQuery(this).data('quantity')+1;
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            action: "update",
            movieId: movieId,
            quantity: quantity,
        },
        success: resultData => {
            console.log("Increased movie quantity");
            $(this).parents('tr').find('a').text(quantity);
            jQuery(this).data('quantity', quantity);
            jQuery(this).siblings('.decrease-quantity').data('quantity', quantity);
            calculateTotalPrice(resultData);
            $('#total-price').text('Total Price: $'+totalPrice);
        },
        error: function (xhr, status, error) {
            // Handle the error if the request fails
            console.log("Error increasing movie quantity:" + error);
        }
    });
});