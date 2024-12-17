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
 * Once this .js is loaded, following scripts will be executed by the browser\
 */


var title = getParameterByName('title');
var year = getParameterByName('year');
var director = getParameterByName('director');
var star = getParameterByName('star');
var limit = getParameterByName('limit');



$(document).ready(function() {
    function getParameterByName(name) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    }

    function setParameterByName(name, value) {
        const url = new URL(window.location);
        if (value) {
            url.searchParams.set(name, value);
        } else {
            url.searchParams.delete(name);
        }
        window.history.pushState({}, '', url);
    }

    $('#prev-page, #next-page').on('click', function() {
        let page = parseInt(getParameterByName('page')) || 1;

        if ($(this).attr('id') === 'prev-page' && page > 1) {
            setParameterByName('page', page - 1);
            page--;
        } else if ($(this).attr('id') === 'next-page') {
            setParameterByName('page', page + 1);
            page++;
        }

        const title = getParameterByName('title');
        const year = getParameterByName('year');
        const director = getParameterByName('director');
        const star = getParameterByName('star');
        const limit = getParameterByName('limit' || 10);

        var queryParams = [];

        if (title) queryParams.push(`title=${encodeURIComponent(title)}`);
        if (year) queryParams.push(`year=${encodeURIComponent(year)}`);
        if (director) queryParams.push(`director=${encodeURIComponent(director)}`);
        if (star) queryParams.push(`star=${encodeURIComponent(star)}`);
        if (limit) queryParams.push(`limit=${encodeURIComponent(limit)}`);
        if (page) queryParams.push(`page=${encodeURIComponent(page)}`);

        var queryString = queryParams.length ? '?' + queryParams.join('&') : '';

        $.ajax({
            dataType: "json",
            method: "GET",
            url: "api/search"  + queryString,
            data: { title, year, director, star, limit, page },
            success: function(resultData) {
                $('#movie_table tbody').empty();
                $('html, body').animate({ scrollTop: 0 }, 600);
                handleMovieResult(resultData);
            },
            error: function(xhr, status, error) {
                // Handle errors here
                console.error("Error fetching results:", error);
            }
        });
    });
});

// Makes the HTTP GET request and registers on success callback function handleResult
var queryParams = [];

if (title) queryParams.push(`title=${encodeURIComponent(title)}`);
if (year) queryParams.push(`year=${encodeURIComponent(year)}`);
if (director) queryParams.push(`director=${encodeURIComponent(director)}`);
if (star) queryParams.push(`star=${encodeURIComponent(star)}`);
if (limit) queryParams.push(`limit=${encodeURIComponent(limit)}`);

// Join parameters to form the query string
var queryString = queryParams.length ? '?' + queryParams.join('&') : '';

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",     // Setting request method
    url: "api/search" + queryString, // Setting request URL
    success: (resultData) => handleMovieResult(resultData) // Callback function to handle returned data
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