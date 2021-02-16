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
	document.getElementById("title").innerHTML = vals.title;
	document.getElementById("author").innerHTML = vals.author;
	document.getElementById("body").innerHTML = vals.body;
}

//api/replies/{id}

let testArr = [["A0",
	["B0",
		["C0"], ["C1"]],
	["B1"]],

["A1",
	["B0",
		["C0"]],
	["B1"]]];

let num = -2;
let display = "";

function formatReplies(replyArr) {
	num++;
	if (Array.isArray(replyArr)) {
		for (let i = 0; i < replyArr.length; i++) {
			if (!Array.isArray(replyArr[i])) {
				load(replyArr[i], num, i);
			}
			formatReplies(replyArr[i]);
			num--;
		}
	}
}

function load(reply, number, id) {
	for (let i = 0; i < number; i++) {
		reply = "    " + reply + " " + id + " " + number;
	}
	display += (reply + "\n");
}

formatReplies(testArr);
console.log(display);