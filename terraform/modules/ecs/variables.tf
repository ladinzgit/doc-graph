variable "cluster_name" {
  type = string
}

variable "ecr_repository_url" {
  type = string
}

variable "image_tag" {
  type = string
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "sg_id" {
  type = string
}

variable "target_group_arn" {
  type = string
}

variable "rds_endpoint" {
  type      = string
  sensitive = true
  default   = ""
}

variable "rds_password" {
  type      = string
  sensitive = true
  default   = ""
}

variable "notion_client_id" {
  type      = string
  sensitive = true
  default   = ""
}

variable "notion_client_secret" {
  type      = string
  sensitive = true
  default   = ""
}

variable "ai_openai_api_key" {
  type      = string
  sensitive = true
  default   = ""
}

variable "ai_openai_base_url" {
  type    = string
  default = "https://api.openai.com"
}

variable "ai_openai_model" {
  type    = string
  default = ""
}
