package LukeS26.github.io.dataschema;

import org.bson.Document;

public class ConfirmationKey extends DataSchema {
	public String key;
	public String username;
	// TODO: Maybe make each key expire?

	@Override
	public Document toDoc() {
		return new Document("key", key).append("username", username);
	}

	public static ConfirmationKey fromDoc(Document doc) {
		ConfirmationKey c = new ConfirmationKey();
		c.key = (String) doc.get("key");
		c.username = (String) doc.get("username");

		return c;
	}
}
