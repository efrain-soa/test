package api.rest.machinne.usuarios.controller;


import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties.Credential;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import api.rest.machinne.usuarios.models.dao.UsuarioDao;
import api.rest.machinne.usuarios.models.documents.Usuario;
import api.rest.machinne.usuarios.util.HashPassword;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController 
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioRestController {
	
	@Autowired
	private UsuarioDao dao;
	
	
	@PostMapping("/crear")
	public Mono<ResponseEntity<Map<String, Object>>> crear( @RequestBody Mono<Usuario> monoUsuario){
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return monoUsuario.flatMap(usuario -> {
			if(usuario.getCreateAt()==null) {
				usuario.setCreateAt(new Date());
			}
			
			String hashPassword = HashPassword.hashPassword(usuario.getHashPassword());
			usuario.setHashPassword(hashPassword);
			
			return dao.save(usuario).map(p-> {
				Usuario usr = new Usuario();
				usr.setId(p.getId());
				usr.setApellidos(p.getApellidos());
				usr.setRol(p.getRol());
				usr.setUsername(p.getUsername());
				usr.setNombre(p.getNombre());
				usr.setEmail(p.getEmail());
				
				
				respuesta.put("status", HttpStatus.CREATED.value());
				respuesta.put("timestamp", new Date());
				respuesta.put("mensaje", "Usuario creado con éxito");
				respuesta.put("usuario", usr);
				
				return ResponseEntity
					.created(URI.create("/api/usuarios/".concat(p.getId())))
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.body(respuesta);
				});
			
		}).onErrorResume(t -> {
			return Mono.just(t).cast(WebExchangeBindException.class)
					.flatMap(e -> Mono.just(e.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "El campo "+fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.flatMap(list -> {
						respuesta.put("errors", list);
						respuesta.put("timestamp", new Date());
						respuesta.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(respuesta));
					});
							
		});
	}
	
	@PostMapping("/aunteticar")
	public Mono<ResponseEntity<Map<String, Object>>> auntenticar( @RequestBody Mono<Credential> monoCredentials){
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("autenticado",false);
		respuesta.put("timestamp", new Date());
		respuesta.put("mensaje", "Usuario y/o password incorrectos.");
		
		return monoCredentials.flatMap(usuario -> {
			
			
			String hashPassword = HashPassword.hashPassword(usuario.getPassword());
		
			
			return dao.findByUsernameAndHashPassword(usuario.getUsername(),hashPassword).map(p-> {
				
			
				
				if(p!=null) {
					
					Usuario usr = new Usuario();
					usr.setId(p.getId());
					usr.setApellidos(p.getApellidos());
					usr.setRol(p.getRol());
					usr.setUsername(p.getUsername());
					usr.setNombre(p.getNombre());
					usr.setEmail(p.getEmail());
					
					respuesta.put("status", HttpStatus.OK.value());
					respuesta.put("autenticado",true);
					respuesta.put("timestamp", new Date());
					respuesta.put("mensaje", "Autenticado con exito");
					respuesta.put("usuario", usr);
				}
				
				
				
				return ResponseEntity
					.accepted()
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.body(respuesta);
				});
							
		}).defaultIfEmpty(
		
			 ResponseEntity.accepted().body(respuesta)
		);
	}
}
