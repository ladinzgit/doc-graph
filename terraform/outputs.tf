output "alb_dns_name" {
  description = "ALB DNS 주소. Notion Webhook 등록 시 사용."
  value       = module.alb.dns_name
}

output "swagger_ui_url" {
  description = "Swagger UI 접속 주소. context-path(/api) 포함."
  value       = "http://${module.alb.dns_name}/api/swagger-ui/index.html"
}

output "ecr_repository_url" {
  description = "Docker 이미지 push 대상 URL"
  value       = module.ecr.repository_url
}

output "rds_endpoint" {
  description = "Spring Boot DB 연결 주소"
  value       = module.rds.endpoint
  sensitive   = true
}

# output "cloudfront_domain" {
#   description = "React 프론트엔드 접속 URL"
#   value       = module.s3_cf.cloudfront_domain
# }
