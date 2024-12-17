let searchTermMap = new Map();

function handleGenreResult(resultData){
    let rowHTML = "";
    for(let i = 0; i < resultData.length; i++){
        rowHTML += "<a href='single-genre.html?id=" + resultData[i]["genreId"] + "'>" + resultData[i]["genreName"] + "</a><a>&nbsp;&nbsp;&nbsp;</a>";
    }
    jQuery("#genre-column").append(rowHTML);
}

function populateCharacters(){
    let characters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*'];
    let rowHTML = "";
    for(let i = 0; i < characters.length; i++){
        rowHTML += "<a href='single-starting-letter.html?chr=" + characters[i] + "'>" + characters[i] + "</a><a>&nbsp;&nbsp;&nbsp;</a>";
    }
    jQuery("#title-column").append(rowHTML);
}

$(document).ready(function() {
    populateCharacters();

    $(document).on('click', '.search-button', function(event) {
        event.preventDefault();

        const title = $('#autocomplete').val();
        const year = $('#yearInput').val();
        const director = $('#directorInput').val();
        const star = $('#starInput').val();
        const limit = $('#limitSelect').val();

        const params = {};
        if (title) params.title = title;
        if (year) params.year = year;
        if (director) params.director = director;
        if (star) params.star = star;
        params.limit = limit;

        // Construct the query string
        const queryString = $.param(params);
        window.location.href = `search.html?${queryString}`;
    });
});

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/all-genres",
    success: (resultData) => {
        console.log(resultData);
        console.log("Populating Genre Result data");
        handleGenreResult(resultData);
    },
    error: function(xhr, status, error) {
        // Handle the error if the request fails
        alert('Failed to populate genre data.');
        console.log(error);
    }
});


$('#autocomplete').autocomplete({
    lookup: function (query, done) {
        handleLookup(query, done)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    deferRequestBy: 300,
    minChars: 3,
});

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    if(searchTermMap.has(query)){
        console.log("Using cached suggestions")
        handleLookupAjaxSuccess(searchTermMap.get(query), query, doneCallback)
    }
    else {
        console.log("Getting Suggestions from DB")
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "api/autocomplete?searchTerm=" + query,
            "success": function (data) {
                searchTermMap.set(query, data);
                handleLookupAjaxSuccess(data, query, doneCallback)
            },
            "error": function (errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup successful")
    console.log(data)
    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}
function handleSelectSuggestion(suggestion) {
    window.location.href = 'single-movie.html?id=' + suggestion["data"]["movieId"]
}