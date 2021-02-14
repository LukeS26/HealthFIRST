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
	let posts = "";
	fetch(url)
		.then(res => res.json())
		.then(json => console.log(json))
		.catch (function (error) {
		console.log(error);
	});

	return(posts);
}

function displayPost(post) {
	let container = document.createElement("div"); 
	let title = document.createElement("div");
	let body = document.createElement("div");
	let user = document.createElement("div");
	container.appendChild(title);
	container.appendChild(body);
	title.appendChild(user);

	let titleText = document.createElement("h1");
	titleText.innerHTML = post.title;
	title.appendChild(titleText);

	let username = document.createElement("h3");
	username.innerHTML = post.author;
	user.appendChild(username);

	let bodyText = document.createElement("p");
	bodyText.innerHTML = post.body;
	body.appendChild(bodyText); 

	document.getElementById("posts").appendChild(container);
}

/*
var request = new Request(url, {
	method: 'POST',
	body: data,
	headers: new Headers()
});
*/