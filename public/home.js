let postCount = 0;
let open = [];

function getPosts(url) {
	let fetchUrl = "157.230.233.218:8080/api/posts/" + url;
	fetch(fetchUrl)
		.then(res => res.json())
		.then(function (json) {
			displayPost(json, url);
		})
		.catch(function (error) {
			console.log(error);

			return null;
		});
}

function displayPost(post, id) {

	let html = "";
	let container = document.createElement("div");
	container.className = "postContainer";
	container.id = "post" + postCount;

	html += `<div id="postOpen" onclick="loadPost('${id}')"> <h1 class='postTitle'>${post.title}</h1>`;
	html += `<h5 class='postAuthor'>${post.author}</h5>`;
	html += `<p class='postBody'>${post.body}</p> </div>`;
	html += `<div class='commentOpener' onclick='toggleCommenter(\"post${postCount}\")'><img src='Arrow.png' width='20px' height='10px'> Comment</div>`;
	html += `<div class='commentCreator' contenteditable>`;

	container.innerHTML += html;


	document.getElementById("posts").appendChild(container);
	postCount++;
}

function loadPost(id) {
	window.location.assign("/post.html?id=" + id);
}

function toggleCommenter(id) {
	let div = document.getElementById(id).childNodes;
	let img = div[1].childNodes;
	img = img[0];

	if (open[id]) {
		img.style.transform = "rotate(0deg)";
		div[2].style.display = "none";
		open[id] = false;
	} else {
		img.style.transform = "rotate(-180deg)";
		div[2].style.display = "block";
		div[2].focus();
		open[id] = true;
	}
}

/*
var request = new Request(url, {
	method: 'POST',
	body: data,
	headers: new Headers()
});
*/

getPosts("602878639903f175355bd339");