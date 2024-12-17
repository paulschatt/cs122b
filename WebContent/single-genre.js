function getParameterByName( name ){
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
    var regexS = "[\\?&]"+name+"=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( window.location.href );
    if( results == null )
        return "";
    else
        return decodeURIComponent(results[1].replace(/\+/g, " "));
}

function handleResultData(resultData){
    let movieTableBodyElement = jQuery("#movie_table_body");
    for(let i = 0; i < resultData.length; i++){
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>";
        let link = "single-movie.html?id=" + resultData[i]["movieId"];
        rowHTML += `<a href="${link}">`;
        rowHTML += resultData[i]["movieTitle"];
        rowHTML += "</a>"
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "<th>";
        for(let j = 0; j < resultData[i]["movie_genres"].length; j++) {
            let link = "single-genre.html?id=" + resultData[i]["genres_id"][j];
            rowHTML += `<a href="${link}">`;
            rowHTML += resultData[i]["movie_genres"][j] + "<br>";
        }
        rowHTML += "</th>";
        rowHTML += "<th>";
        for(let j = 0; j < resultData[i]["movie_stars"].length; j++){
            let link = "single-star.html?id=" + resultData[i]["stars_id"][j];
            rowHTML += `<a href="${link}">`;
            rowHTML += resultData[i]["movie_stars"][j] + "<br>";
        }
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["price"] +"</th>";
        rowHTML += "<th><button type ='button' class='btn btn-dark add-to-cart' data-movie-id='" + resultData[i]['movieId'] + "' data-movie-price='" + resultData[i]['price'] + "'>Add to Cart</button></th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}

jQuery(document).on('click', '.add-to-cart', function(event) {
    // Get the movie ID from the clicked button
    let movieId = jQuery(this).data('movie-id');
    let price = jQuery(this).data('movie-price')
    // Make an AJAX POST request to add the movie to the cart
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            action:"add",
            movieId: movieId,
            quantity: "1",
            price: price
        },
        success: function(responseData) {
            console.log("Added movie to cart");
        },
        error: function(xhr, status, error) {
            // Handle the error if the request fails
            alert('Failed to add movie to cart. Please try again.');
            console.log(error);
        }
    });
});


let genreId = getParameterByName('id');
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-genre?id=" + genreId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => {
        handleResultData(resultData);
    } // Setting callback function to handle data returned successfully by the SingleStarServlet
});