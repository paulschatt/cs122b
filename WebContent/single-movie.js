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
let genres = ["Action", "Adult", "Adventure", "Animation","Biography", "Comedy","Crime",
    "Documentary","Drama","Family", "Fantasy", "History", "Horror", "Music", "Musical", "Mystery", "Reality-TV","Romance","Sci-Fi","Sport","Thriller","War","Western"];


let price;

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


    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    let movieNameElement = jQuery("#movie_name");
    movieNameElement.append(resultData[0]["movie_title"]);
    price = resultData[0]["movie_price"];
    const rowNames = ["Release Year", "Director", "Genres", "Stars", "Rating"]
    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + rowNames[0] + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_year"] + "</th>";
    rowHTML += "</tr>"
    rowHTML += "<tr>";
    rowHTML += "<th>" + rowNames[1] + "</th>";
    rowHTML += "<th>" + resultData[0]["movie_director"] + "</th>";
    rowHTML += "</tr>";
    rowHTML += "<tr>";
    let genres_id = resultData[0]["genres_id"];
    rowHTML += "<th>" + rowNames[2]+ "</th>";
    rowHTML += "<th>";
    for(let j = 0; j < Math.min(3, genres_id.length); j++) {
        let link = "single-genre.html?id=" + genres_id[j];
        rowHTML += `<a href='${link}'>`;
        rowHTML += genres[genres_id[j] - 1];
        rowHTML += "</a>"
        rowHTML += "</br>";
    }
    rowHTML += "</th>";
    rowHTML += "</tr>";
    let stars = resultData[0]["movie_stars"];
    let stars_id = resultData[0]["stars_id"];
    rowHTML += "<tr>";
    rowHTML += "<th>" + rowNames[3]+ "</th>";
    rowHTML += "<th>"
    for(let j = 0; j < stars.length; j++) {
        let link = "single-star.html?id=" + stars_id[j];
        rowHTML += `<a href="${link}">`;
        rowHTML += stars[j];
        rowHTML += "</a>"
        rowHTML += "<br>";
    }
    rowHTML += "</th>";
    rowHTML += "</tr>";
    rowHTML += "<tr>";
    rowHTML += "<th>" + rowNames[4]+ "</th>";
    rowHTML += "<th>" + resultData[0]["movie_rating"] + "</th>";
    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

jQuery(document).on('click', '.add-to-cart', function(event) {
    // Get the movie ID from the clicked button
    console.log(price);
    // Make an AJAX POST request to add the movie to the cart
    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data: {
            action:"add",
            movieId: movieId,
            quantity: "1",
            price: price,
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