function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}


let id = getUrlVars()["id"];

function getPost(url) {
	let fetchUrl = "http://157.230.233.218:8080/api/posts/" + url;
	fetch(fetchUrl)
		.then(res => res.json())
		.then( function(json) {
			displayPost(json);
		})
		.catch (function (error) {
		console.log(error);

		return null;
	});
}

getPost(id);

function displayPost(vals) {
	document.getElementById("title") = vals.title;
	document.getElementById("author") = vals.author;
	document.getElementById("body") = vals.body;
}