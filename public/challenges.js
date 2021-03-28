function getChallenges(page) {
	fetch(`http://157.230.233.218:8080/api/challenges/feed?page=${page}`)
	.then(res => res.json()).then(function(json) {
		console.log(json);
		console.log(json["feed"])
		for(let i = 0; i < json["feed"]; i++) {
			console.log(json["feed"]);
		}
	});
}