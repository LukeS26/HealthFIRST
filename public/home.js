let postCount = 0;
let open = [];

function getPosts(url) {
	let fetchUrl = "http://157.230.233.218:8080/api/posts/" + url;
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

	container.innerHTML += html;


	document.getElementById("posts").appendChild(container);
	postCount++;
}

function loadPost(id) {
	window.location.assign("/post.html?id=" + id);
}

getPosts("602878639903f175355bd339");