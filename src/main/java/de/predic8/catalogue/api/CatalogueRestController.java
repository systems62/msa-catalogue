package de.predic8.catalogue.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.predic8.catalogue.event.Operation;
import de.predic8.catalogue.model.Article;
import de.predic8.catalogue.repository.ArticleRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/articles")
public class CatalogueRestController {

	private ArticleRepository repo;
	private KafkaTemplate<String, Operation> kafka;
	final private ObjectMapper mapper;

	public CatalogueRestController(ArticleRepository repo, KafkaTemplate<String, Operation> kafka, ObjectMapper mapper) {
		this.repo = repo;
		this.kafka = kafka;
		this.mapper = mapper;
	}

	@GetMapping
	public List<Article> index() {
		return repo.findAll();
	}

	@GetMapping("/count")
	public long count() {
		return repo.count();
	}

	@PostMapping
	public ResponseEntity<Article> createArticles(@RequestBody Article entity) {
		
		String uuid = UUID.randomUUID().toString();
		entity.setUuid(uuid);

		Operation op = new Operation("article", "upsert", mapper.valueToTree(entity));
		kafka.send("shop", op);
	
		return ResponseEntity.accepted().build();
	}
	
}