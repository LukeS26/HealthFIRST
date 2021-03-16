function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}


let id = getUrlVars()["id"];
let comments = [];

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

function getComments(url) {
	let fetchUrl = "http://157.230.233.218:8080/api/comments/" + url;
	fetch(fetchUrl)
	.then(res => res.json())
	.then(function (json) {
		console.log(json);
		comments = json["comments"];
	});
}

getPost(id);

function getChildComments(comment) {
	let returnComments = [];

	for(let i = 0; i < comments.length; i++) {
		if(comments[i]["reply_to_id"] != null) {
			if(comments[i]["reply_to_id"]["$oid"] == comment["_id"]["$oid"]) {
				returnComments.push(comments[i]);
				returnComments.push(getChildComments(comments[i]));
			}
		}
	}

	return returnComments;
}

function displayComments() {
	getComments(id);

	let commentsDisplay = [];

	for(let i = 0; i < comments.length; i++) {
		if(comments[i]["reply_to_id"] == null) {
			let temp = getChildComments(comments[i]);
			commentsDisplay.push(comments[i]);
			commentsDisplay.push(temp);
		}
		
	}

	console.log(commentsDisplay);

}

function displayPost(vals) {
	document.getElementById("title").innerHTML = vals.title;
	document.getElementById("author").innerHTML = vals.author;
	document.getElementById("date").innerHTML = new Date(vals.date.$date).toLocaleString();
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
	["B1",
		["C0",	["D0"]], ["C1"]]
	]
];

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
	let comment = `<div name="${number}" style="transform: translateX(${30 * number}px)" > <h4> ${reply} </h4> </div> `
	
	let shell = document.getElementById("comments");

	if(number > 0) {
		//find number - 1, and be that elements child
		let parentComment = document.getElementsByName(number - 1);
		parentComment[parentComment.length - 1].innerHTML += comment;
	} else {
		//be on outside edge
		shell.innerHTML += comment;
	}
}

formatReplies(testArr);
console.log(display);