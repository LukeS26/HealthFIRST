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
				load(replyArr[i], num);
			}
			formatReplies(replyArr[i]);
			num--;
		}
	}
}

function load(reply, number) {
	for (let i = 0; i < number; i++) {
		reply = "    " + reply;
	}
	display += (reply + "\n");
}

formatReplies(testArr);
console.log(display);


function getPosts(url) {
	let fetchUrl = "http://157.230.233.218:8080/api/posts/" + url;
	fetch(fetchUrl)
		.then(res => res.json())
		.then( function(json) {
			displayPost(json, url);
		})
		.catch (function (error) {
		console.log(error);

		return null;
	});
}

function displayPost(post, id) {
	let container = document.createElement("div"); 
	let title = document.createElement("div");
	let body = document.createElement("div");
	let user = document.createElement("div");
	container.appendChild(title);
	container.appendChild(body);

	title.id = "title";
	body.id = "body";
	user.id = "author";

	container.style = "cursor: pointer;";
	title.classList = ["title"];
	body.classList = ["body"];
	user.classList = ["author"];

	let titleText = document.createElement("h1");
	titleText.innerHTML = post.title;
	title.appendChild(titleText);

	title.appendChild(user);

	let username = document.createElement("h5");
	username.innerHTML = post.author;
	user.appendChild(username);

	let bodyText = document.createElement("p");
	bodyText.innerHTML = post.body;
	body.appendChild(bodyText); 

	document.getElementById("posts").appendChild(container);

	container.onclick = function() { loadPost(id) };
}

/*
var request = new Request(url, {
	method: 'POST',
	body: data,
	headers: new Headers()
});
*/

getPosts("602878639903f175355bd339");

function loadPost(id) {
	window.location.assign(window.location.href + "post.html?id=" + id);
}