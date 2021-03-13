let postCount = 0;
let open = [];
let page = 0;

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

	//caps post to 500 chars in home menu
	let body = post.body.slice(0, 500);
	if (body.length === 500) {
		body += "...";
	}
	
	html += `<div id="postOpen" onclick="loadPost('${id}')"><h1 class='postTitle'>${post.title}</h1>`;
	html += `<h5 class='postAuthor'>${post.author}</h5>`;
	html += `<p class='postBody'>${body}</p> </div>`;

	container.innerHTML += html;


	document.getElementById("posts").appendChild(container);
	postCount++;
}

function loadPost(id) {
	window.location.assign("/post.html?id=" + id);
}


//getPosts("602878639903f175355bd339");

function loadPage(page) {
	fetch("http://157.230.233.218:8080/api/posts/feed?page=" + page)
	.then(res => res.json())
	.then(function(json) { 
		for(let i = 0; i < json["feed"].length; i++) {
			console.log(json["feed"][i]);
			displayPost(json["feed"][i], json["feed"][i]["_id"]["$oid"])
		}
	} );
}

loadPage(0);

window.onscroll = function(ev) {
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
		page++;
		loadPage(page);
    }
};