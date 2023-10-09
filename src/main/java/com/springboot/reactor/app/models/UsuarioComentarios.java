package com.springboot.reactor.app.models;

public class UsuarioComentarios {

	private Usuario usuario;
	
	private Comentarios comentarios;
	
	public UsuarioComentarios(Usuario usuario, Comentarios comentarios) {
		this.usuario = usuario;
		this.comentarios = comentarios;
	}	
	
	@Override
	public String toString() {
		return "UsuariosComentarios [usuario=" + usuario + ", comentarios=" + comentarios + "]";
	}
}
