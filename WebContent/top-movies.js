/**
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating star table from resultData");
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    console.log(resultData)
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] + // display movie_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
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
        rowHTML += "<th>" + resultData[i]["movie_rating"] +  "</th>";
        rowHTML += "<th><button type ='button' class='btn btn-dark add-to-cart' data-movie-id='" + resultData[i]['movie_id'] + "' data-movie-price='" + resultData[i]['movie_price'] + "'>Add to Cart</button></th>";
        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top-movies",
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the Servlet
});
// Use jQuery's event delegation to listen for clicks on dynamically created buttons
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