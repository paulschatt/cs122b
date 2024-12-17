function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
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


let chr = getParameterByName("chr");
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url:"api/single-starting-letter?chr=" + chr,
    success: resultData => {
        handleResultData(resultData);
        console.log("Fetching movies for single letter");
    },
    error: function(xhr, status, error) {
        // Handle the error if the request fails
        alert('Failed to add movie to cart. Please try again.');
        console.log(error);
    }
})

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