let employee_login_form = $("#employee_login_form");
let insert_star_form = $("#insert_star_form");
let insert_movie_form = $("#insert_movie_form")

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */

function displayMetadata(tables) {
    console.log(tables); // Log the tables data for debugging

    // Start building the HTML for tables
    let html = "<ul>";

    // Loop through each table in the tables array
    tables.forEach(table => {
        html += "<li><strong>Table:</strong> " + table.tableName + "<ul>";

        // Loop through each column (attribute) for the current table
        table.columns.forEach(attr => {
            html += "<li><strong>Column:</strong> " + attr.columnName + ", <strong>Type:</strong> " + attr.dataType + "</li>";
        });

        html += "</ul></li>";
    });

    html += "</ul>";

    // Inject the generated HTML into the DOM element with id 'metadata'
    $("#metadata").html(html);
}


function loadPageOnSuccess() {
    $("#employee_login_form").hide();
    $('#content').show();
    $.ajax(
        "api/metadata", {
            method: "GET",
            dataType: "json",
            // Serialize the login form to the data sent by POST request
            success: function (data) {
                const tables = data?.tables ?? [];
                displayMetadata(tables);
            },
            error: function (xhr, status, error) {
                $("#metadata").html("<p>Error retrieving metadata: " + error + "</p>");
            }
        }
    );
}

function handleEmployeeLoginResult(resultDataString) {
    console.log("handle employee response");
    console.log(resultDataString)
    let resultDataJson = resultDataString;

    console.log("handle employee response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to top-movies.html
    if (resultDataJson["status"] === "success") {
        loadPageOnSuccess()
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        $("#employee_error_message").text("Incorrect username or password.");
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitEmployeeLoginForm(formSubmitEvent) {
    console.log("submit employee form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/employee-login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: employee_login_form.serialize(),
            success: handleEmployeeLoginResult,
            error: function (xhr, status, error){
                console.log(status);
                console.log(xhr);
                console.log(error);
                if(xhr["status"]===400) {
                    alert("Login Error: Please verify you are human");
                }
            }
        }
    );
}

function submitInsertStarForm(formSubmitEvent) {
    console.log("inserting star");
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/insert-star", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: insert_star_form.serialize(),
            success: $("#star_message").text("Added star."),
            error: function (xhr, status, error){
                console.log(status);
                console.log(xhr);
                console.log(error);
            }
        }
    );
}

function submitInsertMovieForm(formSubmitEvent){
    console.log("inserting movie");
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/insert-movie",{
            method: "POST",
            data: insert_movie_form.serialize(),
            success: $("#movie_message").text("Added Movie"),
            error: function (xhr, status, error){
                console.log(status);
                console.log(xhr);
                console.log(error);
            }
        }
    )
}

// Bind the submit action of the form to a handler function
employee_login_form.submit(submitEmployeeLoginForm);
insert_star_form.submit(submitInsertStarForm);
insert_movie_form.submit(submitInsertMovieForm);