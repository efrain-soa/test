package api.rest.machinne.usuarios.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import api.rest.machinne.usuarios.models.documents.Usuario;
import reactor.core.publisher.Mono;

public interface UsuarioDao extends ReactiveMongoRepository<Usuario, String>{
	
	 Mono<Usuario> findByUsernameAndHashPassword (String username, String hashPassword);

}

