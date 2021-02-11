function Chat(chatTitle, chatBody, replies) {
	this.title = chatTitle;
	this.body = chatBody;
	this.replies = replies;
}

let testArr = [ ["A0", 
					["B0", 
						["C0"], ["C1"]], 
					["B1"] ], 

				["A1", 
					["B0", 
						["C0"]], 
					["B1"] ] ];

function formatReplies(replyArr) {
	for(let i = 0; i < replyArr.length; i++) {
		load(replyArr[i]);
		formatReplies(replyArr[i]);
	}
}

function load(arr) {
	console.log(arr);
}