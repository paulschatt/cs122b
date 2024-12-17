/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starNameElement = jQuery("#star_name");
    starNameElement.append(resultData[0]["star_name"]);
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Date Of Birth: " + resultData[0]["star_year"] + "</p>");

    console.log("handleResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>";
        let link = "single-movie.html?id=" + resultData[i]["movie_id"];
        rowHTML += `<a href="${link}">`;
        rowHTML += resultData[i]["movie_title"];
        rowHTML += "</a>"
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
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
        rowHTML += "<th>";
        rowHTML += "<button type ='button' class='btn btn-dark add-to-cart' data-movie-id='" + resultData[i]['movie_id'] + "' data-movie-price='" + resultData[i]['movie_price'] + "'>Add to Cart</button>";
        rowHTML += "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData),
    error: function(xhr, status, error) {
        // Handle the error if the request fails
        alert('Failed to get star data. Please try again.');
        console.log(error);
    }
    // Setting callback function to handle data returned successfully by the SingleStarServlet
});

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